package org.icepdf.core.views.common;

import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.util.GraphicsRenderingHints;
import org.icepdf.core.views.DocumentViewController;
import org.icepdf.core.views.DocumentViewModel;
import org.icepdf.core.views.swing.AbstractPageViewComponent;
import org.icepdf.core.views.swing.AnnotationComponent;

import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * <p>This classes purpose is to manage annotation selected state and the
 * broadcaset of resized and moved for multiple selected components.  The
 * other purpose of this class is to handle the drawing of a selection box
 * and handle the creation of new link annotation when the link annotation
 * tool is selected </p>
 *
 * @since 4.0
 */
public class AnnotationHandler extends SelectionBoxHandler
        implements MouseInputListener {

    private static final Logger logger =
            Logger.getLogger(AnnotationHandler.class.toString());


    // parent page component
    private AbstractPageViewComponent pageViewComponent;
    private DocumentViewController documentViewController;
    private DocumentViewModel documentViewModel;

    // selected annotations.
    private ArrayList<AnnotationComponent> selectedAnnotations;

    public AnnotationHandler(AbstractPageViewComponent pageViewComponent,
                             DocumentViewModel documentViewModel) {
        this.pageViewComponent = pageViewComponent;
        this.documentViewModel = documentViewModel;
        selectedAnnotations = new ArrayList<AnnotationComponent>();
    }

    /**
     * DocumentController callback
     *
     * @param documentViewController document controller.
     */
    public void setDocumentViewController(
            DocumentViewController documentViewController) {
        this.documentViewController = documentViewController;
    }

    /**
     * Creates a new link annotation when the link annotation creation tool is
     * selected.  The bounds of the annotation are defined by the current
     * selection box that has none zero bounds.  If the two previous
     * conditions are met then the annotation callback is fired and an
     * width a new annotation object which can be updated by the end user
     * using either the api or UI tools.
     */
    public void createNewLinkAnnotation() {
        // todo setup objects, add them to library and finally the callback api
        // call.
        if (documentViewModel.getViewToolMode() !=
                DocumentViewModel.DISPLAY_TOOL_SELECTION) {
//            if (documentViewController.getAnnotationCallback() != null) {
//                documentViewController.getAnnotationCallback()
//                        .proccessAnnotationAction(annotation);
//            }
        }
    }

    /**
     * Adds an Annotation component to the list of selected.  The list
     * of selected annotations is used do batch resize and moved commands.
     *
     * @param annotationComponent component to add to list of selected annotations
     */
    public void addSelectedAnnotation(AnnotationComponent annotationComponent) {
        selectedAnnotations.add(annotationComponent);
    }

    /**
     * Adds an Annotation component to the list of selected.  The list
     * of selected annotations is used do batch resize and moved commands.
     *
     * @param annotationComponent remove the specified annotation from the
     *                            selection list
     */
    public void removeSelectedAnnotation(AnnotationComponent annotationComponent) {
        selectedAnnotations.remove(annotationComponent);
    }

    /**
     * Clears the slected list of AnnotationComponent,  PageViewComponent
     * focus should be called after this method is called to insure deselection
     * of all AnnotationComponents.
     */
    public void clearSelectedList() {
        selectedAnnotations.clear();
    }

    /**
     * Determines if there are more then one selected component.  If there is
     * more then one component that steps should be made to do batch move and
     * resize propigation.
     *
     * @return true if there are more then one AnnotationComponents in a selected
     *         state
     */
    public boolean isMultipleSelect() {
        return selectedAnnotations.size() > 1;
    }

    /**
     * Moves all selected annotation components by the x,y translation.
     *
     * @param x x-axis offset to be applied to all selected annotation.
     * @param y y-axis offset to be applied to all selected annotation.
     */
    public void moveSelectedAnnotations(int x, int y) {
        // todo implement

        // considerations to make sure annotation is not outside of page bounds
    }

    /**
     * Resizes all selected annotation components by the width and height
     * values.
     *
     * @param width  width offset to be applied to all selected annotation.
     * @param height height offset to be applied to all selected annotation.
     */
    public void resizeSelectedAnnotations(int width, int height) {
        // todo implement

        // considerations to make sure annotation is not outside of page bounds
    }


    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {

        clearSelectedList();

        // annotation selection box.
        if (documentViewModel.getViewToolMode() ==
                DocumentViewModel.DISPLAY_TOOL_SELECTION) {
            int x = e.getX();
            int y = e.getY();
            currentRect = new Rectangle(x, y, 0, 0);
            updateDrawableRect(pageViewComponent.getWidth(), pageViewComponent.getHeight());
            pageViewComponent.repaint();
        }

    }

    public void mouseDragged(MouseEvent e) {
        if (documentViewModel.getViewToolMode() ==
                DocumentViewModel.DISPLAY_TOOL_SELECTION) {

            // rectangle select tool
            updateSelectionSize(e, pageViewComponent);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (documentViewModel.getViewToolMode() ==
                DocumentViewModel.DISPLAY_TOOL_SELECTION) {

            // update selection rectangle
            updateSelectionSize(e, pageViewComponent);

            // clear the rectangle
            clearRectangle(pageViewComponent);

            pageViewComponent.repaint();
        }
    }

    public void mouseMoved(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    /**
     * Paints all annotation content for a given page view.  If any annotation
     * properties are changed then this method must be called to repaint the
     * page annotations.
     * <p/>
     * todo: as a future enhancement it would be great if each Annotation
     * component did its own painting,  this would take a little more time
     * to figure out the correct coordinate space.
     *
     * @param g parent PageViewComponent graphics context to paint annotations
     *          to.
     */
    public void paintAnnotations(Graphics g) {
        Page currentPage = pageViewComponent.getPageLock(this);
        if (currentPage != null && currentPage.isInitiated()) {
            ArrayList<Annotation> annotations = currentPage.getAnnotations();
            if (annotations != null) {

                Graphics2D gg2 = (Graphics2D) g;

                // save draw state.
                AffineTransform prePaintTransform = gg2.getTransform();
                Color oldColor = gg2.getColor();
                Stroke oldStroke = gg2.getStroke();

                AffineTransform at = currentPage.getPageTransform(
                        documentViewModel.getPageBoundary(),
                        documentViewModel.getViewRotation(),
                        documentViewModel.getViewZoom());
                gg2.transform(at);

                // paint all annotations on top of the content buffer
                Object tmp;
                Annotation annotation;
                for (Object annotation1 : annotations) {
                    tmp = annotation1;
                    if (tmp instanceof Annotation) {
                        annotation = (Annotation) tmp;
                        // todo annotationComp know if they are selected....
                        // and we can paint the selected state. 
                        annotation.render(gg2, GraphicsRenderingHints.SCREEN,
                                documentViewModel.getViewRotation(),
                                documentViewModel.getViewZoom(), false);
                    }
                }
                // post paint clean up.
                gg2.setColor(oldColor);
                gg2.setStroke(oldStroke);
                gg2.setTransform(prePaintTransform);
            }
        }
        pageViewComponent.releasePageLock(currentPage, this);

        // pain selection box
        paintSelectionBox(g);
    }

    /**
     private void annotationMouseMoveHandler(Page currentPage,
     Point mouseLocation) {

     if (currentPage != null &&
     currentPage.isInitiated() &&
     isInteractiveAnnotationsEnabled) {
     ArrayList<Annotation> annotations = currentPage.getAnnotations();
     if (annotations != null) {
     Annotation annotation;
     Object tmp;
     AffineTransform at = currentPage.getPageTransform(
     documentViewModel.getPageBoundary(),
     documentViewModel.getViewRotation(),
     documentViewModel.getViewZoom());

     try {
     at.inverseTransform(mouseLocation, mouseLocation);
     } catch (NoninvertibleTransformException e1) {
     e1.printStackTrace();
     }

     for (Object annotation1 : annotations) {
     tmp = annotation1;
     if (tmp instanceof Annotation) {
     annotation = (Annotation) tmp;
     // repaint an annotation.
     if (annotation.getUserSpaceRectangle().contains(
     mouseLocation.getX(), mouseLocation.getY())) {
     currentAnnotation = annotation;
     documentViewController.setViewCursor(DocumentViewController.CURSOR_HAND_ANNOTATION);
     //                            repaint(annotation.getUserSpaceRectangle().getBounds());
     pageViewComponent.repaint();
     break;
     } else {
     currentAnnotation = null;
     }
     }
     }
     if (currentAnnotation == null) {
     int toolMode = documentViewModel.getViewToolMode();
     if (toolMode == DocumentViewModel.DISPLAY_TOOL_PAN) {
     documentViewController.setViewCursor(DocumentViewController.CURSOR_HAND_OPEN);
     } else if (toolMode == DocumentViewModel.DISPLAY_TOOL_ZOOM_IN) {
     documentViewController.setViewCursor(DocumentViewController.CURSOR_ZOOM_IN);
     } else if (toolMode == DocumentViewModel.DISPLAY_TOOL_ZOOM_OUT) {
     documentViewController.setViewCursor(DocumentViewController.CURSOR_ZOOM_OUT);
     } else if (toolMode == DocumentViewModel.DISPLAY_TOOL_TEXT_SELECTION) {
     documentViewController.setViewCursor(DocumentViewController.CURSOR_SELECT);
     }
     pageViewComponent.repaint();
     }
     }
     }
     }
     */
}
